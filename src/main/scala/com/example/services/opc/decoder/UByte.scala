package com.example.services.opc.decoder


object UByte {

  val VALUES = mkValues()

  /**
    * A constant holding the minimum value an <code>unsigned byte</code> can
    * have, 0.
    */
  val MIN_VALUE = 0x00

  /**
    * A constant holding the maximum value an <code>unsigned byte</code> can
    * have, 2<sup>8</sup>-1.
    */
  val MAX_VALUE = 0xff

  /**
    * A constant holding the minimum value an <code>unsigned byte</code> can
    * have as UByte, 0.
    */
  val MIN: UByte = valueOf(0x00)

  /**
    * A constant holding the maximum value an <code>unsigned byte</code> can
    * have as UByte, 2<sup>8</sup>-1.
    */
  val MAX: UByte = valueOf(0xff)

  /**
    * Get an instance of an <code>unsigned byte</code>
    */
  def valueOf(value: Int): UByte = valueOfUnchecked(rangeCheck(value))

  def valueOf(value: String): UByte = valueOfUnchecked(rangeCheck(value.toShort))

  /**
    * Get the value of a short without checking the value.
    */
  private def valueOfUnchecked(value: Short) = VALUES(value & MAX_VALUE)

  def mkValues(): Array[UByte] = {
    val ret = new Array[UByte](256)
    (Byte.MinValue to Byte.MaxValue).foreach(index => {
      val shrt = (index.asInstanceOf[Byte] & MAX_VALUE).asInstanceOf[Short]
      ret(index & MAX_VALUE) = new UByte(shrt)
    })
    ret
  }

  def fromInt(value: Int): UByte = {
    new UByte(rangeCheck(value))
  }

  def fromString(value: String): UByte = {
    new UByte(rangeCheck(value.toShort))
  }

  private def rangeCheck(value: Int): Option[Short] = {
    if (value < MIN_VALUE || value > MAX_VALUE) None
    else Some(value.toShort)
  }

}
final case class UByte(value: Short)