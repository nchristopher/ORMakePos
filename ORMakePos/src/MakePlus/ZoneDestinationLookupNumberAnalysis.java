package MakePlus;

import OpenRate.lang.DigitTree;
import OpenRate.process.AbstractBestMatch;
import OpenRate.record.RecordError;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import java.beans.DesignMode;
import java.util.ArrayList;

/**
 * This module attempts to retrieve the destination of the event by matching 
 * against the DESTINATION_MAP table using BNumberNorm.
 * 
 * We make up to two accesses into the DESTINATION_MAP table:
 *  - If we have a specific rateplan for this customer, we make one access
 *    into the table using the specific rateplan name as the MAP_GROUP
 *  - In all cases we make an access into the table using the service "TEL"
 *    as the MAP_GROUP
 * 
 * If the specific rateplan does not result in a zone, we will silently
 * accept the base rateplan result (there will be no error). This gives the
 * greatest flexibility of allowing either different or the same zoning
 * granularity.
 */
public class ZoneDestinationLookupNumberAnalysis extends AbstractBestMatch
{
  // this is the CVS version info
  public static String CVS_MODULE_INFO = "OpenRate, $RCSfile: ZoneDestinationLookupNumberAnalysis.java,v $, $Revision: 1.5 $, $Date: 2011/09/22 16:19:09 $";

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

 /**
  * Perform the look up
  */
  @Override
  public IRecord procValidRecord(IRecord r)
  {
    RecordError tmpError;
    String ZoneValue;
    String DestinationValue;
    CDRRecord CurrentRecord = (CDRRecord)r;

    // Look up the destination for the standard zoning for the base rateplan
    try
    {
      // Look up the BNumberNorm
      ZoneValue = getBestMatch(CurrentRecord.Service, CurrentRecord.BNumberNorm);
      DestinationValue = getBestMatchWithChildData(CurrentRecord.Service, CurrentRecord.BNumberNorm).get(1);
     
      // Write the information back into the record
      if (ZoneValue.equalsIgnoreCase(DigitTree.NO_DIGIT_TREE_MATCH))
      {
        // error detected, add an error to the record
        tmpError = new RecordError("ERR_ZONE_DEST_NOT_FOUND", ErrorType.DATA_NOT_FOUND);
        tmpError.setModuleName(getSymbolicName());
        tmpError.setErrorDescription("ZoneDestination Value Not Found for Service:" + CurrentRecord.Service
            + " Destination:" + CurrentRecord.BNumberNorm);
        CurrentRecord.addError(tmpError);
      }
      else
      {
        // the zoning was successful
        CurrentRecord.ZoneDestination = ZoneValue;
        CurrentRecord.DestinationDescription = DestinationValue;
      }
    }
    catch (Exception e)
    {
      tmpError = new RecordError("ERR_ZONE_DEST_FAILED", ErrorType.SPECIAL);
      tmpError.setModuleName(getSymbolicName());
      tmpError.setErrorDescription("ZoneDestination Value Not Found:" + e.getMessage());
      CurrentRecord.addError(tmpError);
    }
      
    // If we are using a specific rateplan override, we will have to look up
    // the zoning for both specific and base - they might have different
    // granularities of zoning
    if (CurrentRecord.specificPlan)
    {
      try
      {
        // Look up the BNumberNorm using the specific tariff identifier
        ZoneValue = getBestMatch(CurrentRecord.UserTariff, CurrentRecord.BNumberNorm);
        //DestinationValue = getBestMatchWithChildData(CurrentRecord.UserTariff, CurrentRecord.BNumberNorm).get(1);
        // If we do not find a specific result, we use the standard value
        if (ZoneValue.equalsIgnoreCase(DigitTree.NO_DIGIT_TREE_MATCH))
        {
          CurrentRecord.SpecZoneDest = CurrentRecord.ZoneDestination;
        }
        else
        {
          CurrentRecord.SpecZoneDest = ZoneValue;
          //CurrentRecord.DestinationDescription = DestinationValue;
        }
      }
      catch (Exception e)
      {
        tmpError = new RecordError("ERR_ZONE_DEST_FAILED", ErrorType.SPECIAL);
        tmpError.setModuleName(getSymbolicName());
        tmpError.setErrorDescription("ZoneDestination Value Not Found:" + e.getMessage());
        CurrentRecord.addError(tmpError);
      }
    }

    return r;
  }

  /**
  * This is called when a data record with errors is encountered. You should do
  * any processing here that you have to do for error records, e.g. stratistics,
  * special handling, even error correction!
  */
  @Override
  public IRecord procErrorRecord(IRecord r)
  {
    return r;
  }
}
