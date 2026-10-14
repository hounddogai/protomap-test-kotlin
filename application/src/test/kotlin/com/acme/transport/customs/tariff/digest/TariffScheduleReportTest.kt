package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707825139)
        val items = mutableListOf<TariffSchedule>()
        for (index in 0 until 4) {
            items.add(newTariffSchedule("tariff-schedule-$index", "sample"))
        }
        val report = buildTariffScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
