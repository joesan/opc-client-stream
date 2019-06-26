package com.example.services.opc.decoder

import java.util.UUID
import org.joda.time._

/**
 * Translate this:
 *   https://github.com/open62541/open62541/blob/master/src/pubsub/ua_pubsub_networkmessage.h
 *   https://github.com/open62541/open62541/blob/master/src/ua_types.c
 */
object OpcUaPubSubTypes {

  sealed trait PublisherIdDataType
  object PublisherIdDataType {
    case object BYTE extends PublisherIdDataType
    case object UINT16 extends PublisherIdDataType
    case object UINT64 extends PublisherIdDataType
    case object STRING extends PublisherIdDataType
  }

  sealed trait DataSetMessageType
  object DataSetMessageType {
    case object DATAKEYFRAME extends DataSetMessageType
    case object DATADELTAFRAME extends DataSetMessageType
    case object EVENT extends DataSetMessageType
    case object KEEPALIVE extends DataSetMessageType
  }

  sealed trait FieldEncoding
  object FieldEncoding {
    case object VARIANT extends FieldEncoding
    case object RAWDATA extends FieldEncoding
    case object DATAVALUE extends FieldEncoding
  }

  sealed trait NetworkMessageType
  object NetworkMessageType {
    case object DATASET extends NetworkMessageType
    case object REQUEST extends NetworkMessageType
    case object RESPONSE extends NetworkMessageType
  }

  case class NetworkMessageGroupHeader(
    writerGroupIdEnabled: Boolean,
    groupVersionEnabled: Boolean,
    networkMessageNumberEnabled: Boolean,
    sequenceNumberEnabled: Boolean,
    writerGroupId: Short, // UINT16
    groupVersion: Int, // UINT32
    networkMessageNumber: Short, // UINT16
    sequenceNumber: Int // UINT16
  )

  case class DataSetMessageHeader(
    dataSetMessageValid: Boolean,
    fieldEncoding: FieldEncoding,
    dataSetMessageSequenceNrEnabled: Boolean,
    timestampEnabled: Boolean,
    statusEnabled: Boolean,
    configVersionMajorVersionEnabled: Boolean,
    configVersionMinorVersionEnabled: Boolean,
    dataSetMessageType: DataSetMessageType,
    picoSecondsIncluded: Boolean,
    dataSetMessageSequenceNr: Int, // UINT16
    timestamp: DateTime,
    picoSeconds: Int, // UINT16
    status: Int, // UINT16
    configVersionMajorVersion: Int, // UINT32
    configVersionMinorVersion: Int // UINT32
  )

  case class DataValue(
    //value: Variant,
    sourceTimestamp: DateTime,
    serverTimestamp: DateTime,
    sourcePicoseconds: Short,
    serverPicoseconds: Short,
    status: Int,
    hasValue: Boolean,
    hasStatus: Boolean,
    hasSourceTimestamp: Boolean,
    hasServerTimestamp: Boolean,
    hasSourcePicoseconds: Boolean,
    hasServerPicoseconds: Boolean)

  case class DataSetPayloadHeader(count: Byte, dataSetWriterIds: Seq[Short])

  case class DataKeyFrameData(fieldCount: Short, dataSetFields: Seq[DataValue], fieldNames: Seq[String])
  case class DeltaFrameField(fieldIndex: Short, fieldValue: DataValue)
  case class DataDeltaFrameData(fieldCount: Short, deltaFrameFields: Seq[DeltaFrameField])

  case class DataSetPayload(sizes: Seq[Short], dataSetMessages: Seq[DataSetMessage])
  case class DataSetMessage(
    header: DataSetMessageHeader,
    keyFrameData: DataKeyFrameData,
    deltaFrameData: DataDeltaFrameData)

  case class NetworkMessage(
    version: Byte,
    messageIdEnabled: Boolean,
    messageId: String,
    publisherIdEnabled: Boolean,
    groupHeaderEnabled: Boolean,
    payloadHeaderEnabled: Boolean,
    publisherIdType: PublisherIdDataType,
    dataSetClassIdEnabled: Boolean,
    securityEnabled: Boolean,
    timestampEnabled: Boolean,
    picosecondsEnabled: Boolean,
    chunkMessage: Boolean,
    promotedFieldsEnabled: Boolean,
    networkMessageType: NetworkMessageType,
    publisherIdByte: Byte,
    publisherIdUInt16: Int,
    publisherIdUInt32: Int,
    publisherIdGuid: UUID,
    publisherIdString: String,
    dataSetClassId: UUID,
    groupHeader: NetworkMessageGroupHeader,
    dataSetPayloadHeader: DataSetPayloadHeader,
    timestamp: DateTime,
    picoseconds: Int, // UINT16
    promotedFieldsSize: Int, // UINT16
    //promotedFields: Seq[Variant],
    dataSetPayload: DataSetPayload,
    securityFooter: String,
    signature: String)
}
