package latis.time

import org.junit._
import Assert._
import latis.metadata.Metadata
import latis.dm.Real
import latis.dm.Text
import latis.dm.Function
import latis.dm.Dataset
import latis.ops.TimeFormatter
import latis.dm.Sample
import latis.dm.Tuple
import latis.dm.Number
import latis.data.value.DoubleValue

class TestTime {

  @Test
  def iso_to_millis {
    val ms = Time.isoToJava("1970-01-01T00:00:00")
    assertEquals(0, ms)
  }

  @Test
  def iso_with_millis_to_millis {
    val ms = Time.isoToJava("1970-01-01T00:00:00.001")
    assertEquals(1, ms)
  }

  @Test
  def iso_with_millis_to_millis_withZ {
    val ms = Time.isoToJava("1970-01-01T00:00:00.001Z")
    assertEquals(1, ms)
  }
  
  @Test
  //TODO: not supported by javax.xml.bind.DatatypeConverter.parseDateTime
  def iso_without_T_to_millis {
    val ms = Time.isoToJava("1970-01-01 00:00:00")
    assertEquals(0, ms)
  }
  
  @Test
  def iso_date_to_millis {
    val ms = Time.isoToJava("1970-01-01")
    assertEquals(0, ms)
  }
  
 @Test
  def iso_ordinal_date {
    val ms = Time.isoToJava("1970-001")
    assertEquals(0, ms)
  }
 
 @Test
  def iso_ordinal_date_not_month {
    //javax.xml.bind.DatatypeConverter interprets day of year as month
    val ms = Time.isoToJava("1970-002")
    assertEquals(86400000, ms)
  }
  
  @Test
  def iso_ordinal_date_with_time {
    val ms = Time.isoToJava("1970-001T00:00:01")
    assertEquals(1000, ms)
  }
  
  @Test
  def iso_to_iso_ordinal {
    val iso_ord = Time.fromIso("2013-12-04T23:00:00").format("yyyy-DDD'T'HH:mm:ss.SSS")
    assertEquals("2013-338T23:00:00.000", iso_ord)
  }
  
  @Test
  def javaToIso = {
    val iso = Time.javaToIso(0)
    assertEquals("1970-01-01T00:00:00.000", iso)
  }
  
  //TODO: test other flavors, with time zone,...

  
  @Test
  def julian_date {
    val t = Time(TimeScale.JULIAN_DATE, 2456734.5)
    assertEquals("2014-03-18T00:00:00.000", t.format(TimeFormat.ISO))
  }
  
  
  //construction without data, make sure metadata captures defaults
  
  @Test def text_type_with_units = {
    val t = Time("text", Metadata(Map("units" -> "yyyy-MM-dd")))
    assertTrue(t.isInstanceOf[Text])
    assertEquals("10", t.getMetadata("length").get)
    assertEquals("yyyy-MM-dd", t.getUnits.toString) //default numeric units
  }
  
  @Test def text_type_without_units = {
    val t = Time("text", Metadata.empty)
    assertTrue(t.isInstanceOf[Text])
    assertEquals("23", t.getMetadata("length").get)
    assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSS", t.getMetadata("units").get)
    assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSS", t.getUnits.toString) //default numeric units
  }

  //@Test def text_type_with_units_and_length = ???
  //@Test def text_type_with_units_and_length_too_long = ???
  //@Test def text_type_with_units_and_length_too_short = ???
  //@Test def text_type_with_invalid_units = ???
  
  @Test def real_type_with_units = {
    val t = Time("real", Metadata(Map("units" -> "years since 2000-01-01")))
    assertTrue(t.isInstanceOf[Real])
    assertEquals("years since 2000-01-01", t.getUnits.toString)
  }
  
  //@Test def real_type_with_invalid_units = ???
  //@Test def int_type_with_units = ???
  //@Test def int_type_without_units = ???
  //@Test def int_type_with_invalid_units = ???
  
  @Test @Ignore //should throw exception (LATIS-329)
  def time_construction_with_invalid_units = {
    val t = Time(Metadata(Map("units" -> "my days since 2000-01-01")), 14)
    fail
  }
  
  //@Test 
  def fractional_years {
    //TODO: off due to leap days
    val t = Time(Metadata(Map("units" -> "years since 2000-01-01")), 14)
    val s = t.format("yyyy-MM-dd") //2013-12-28
    println(s)
  }
  
  //@Test 
  def time_as_tuple {
//timed_see_ssi_l3a
//    <time units="yyyyDDD ???"/>
//      <text name="DATE" />
//      <real name="TIME" units="seconds"/>
//    </time>
    //TODO: start with date+time only?
    //as many text elements as needed: yyyy, mm, dd, hh...
    //append text vars in order (comma delim?)
    //always convert to java ms? or only if there is a numeric component?
    //add numeric component, converted to ms
    /*
     * can we use the tuple representation in the model (1st pass) then convert to scalar as we parse data?
     * how attached are we to the idea of Time being a scalar?
     *   domain tuple arity is a handy indicator of number of dimensions
     *   (date,time) would be a 1D manifold in this case?
     *   some data tables are 2D (year,month)
     * derived field?
     *   date and time could be in range, groupBy time after
     *   just a sum
     * consider how columnar adapter can stitch diff vars together
     */
    val d = Text(Metadata("Date"), "2014-01-01")
    val t = Real(Metadata(Map("name" -> "Time", "units" -> "seconds")), 123.0)
    //val time = Time(List(d,t), Metadata(Map("units" -> "seconds")))
    
    
  }
  
  @Test
  def second_sixty_as_leap_second_ignored = {
    //Note, this is not ideal. The 60th second more appropriately belongs to the previous day.
    //See http://www.ietf.org/timezones/data/leap-seconds.list
    //Unix seems to do it this way.
    val ms0 = Time.isoToJava("2015-06-30T23:59:59")
    val ms1 = Time.isoToJava("2015-06-30T23:59:60")
    val ms2 = Time.isoToJava("2015-07-01T00:00:00")
    assertEquals(ms1, ms0+1000)
    assertEquals(ms1, ms2)
  }
  
  @Test
  def time_from_formatted_string = {
    val ds = Dataset(Time("2015-10-01"))
    TimeFormatter("yyyy/DDD")(ds) match {
      case Dataset(Text(s)) => assertEquals("2015/274", s)
    }
  }
  
  @Test 
  def time_from_formatted_string_with_metadata_name_only = {
    val ds = Dataset(Time(Metadata("time"), "2015-10-01"))
    TimeFormatter("yyyy/DDD")(ds) match {
      case Dataset(Text(s)) => assertEquals("2015/274", s)
    }
  }
  
  @Test
  def time_from_formatted_string_with_metadata_with_units = {
    val ds = Dataset(Time(Metadata(Map("name" -> "time", "units" -> "yyyy-MM-dd")), "2015-10-01"))
    TimeFormatter("yyyy/DDD")(ds) match {
      case Dataset(Text(s)) => assertEquals("2015/274", s)
    }
  }
  
  @Test
  def unspecified_2_digit_year {
    val ds = Dataset(Time(Metadata(Map("name" -> "time", "units" -> "yy-MM-dd")), "15-10-01"))
    TimeFormatter("yyyy-MM-dd")(ds) match {
      case Dataset(Text(s)) => assertEquals("2015-10-01", s)
    }
  }
  
  @Test
  def specified_2_digit_year {
    val ds = Dataset(Time(Metadata(Map("name" -> "time", "units" -> "yy-MM-dd", "century_start_date" -> "1900")), "15-10-01"))
    TimeFormatter("yyyy-MM-dd")(ds) match {
      case Dataset(Text(s)) => assertEquals("1915-10-01", s)
    }
  }
  
  @Test
  def get_number_data_test {
    val ds = latis.dm.TestDataset.canonical
    ds match {
      case Dataset(Function(it)) => it.next; it.next match {
        case Sample(Number(t), _) => assertEquals(8.64E7, t, 0)
      }
    }
  }
  
  @Test
  def copy_text_time_with_numeric_data = {
    val t1 = Time(Metadata("name" -> "time", "units" -> "yyyy-MM-dd HH:mm:ss"), "")
    val t2 = t1(DoubleValue(1000.0))
    t2 match {
      case Text(s) => assertEquals("1970-01-01 00:00:01", s)
    }
  }
}
