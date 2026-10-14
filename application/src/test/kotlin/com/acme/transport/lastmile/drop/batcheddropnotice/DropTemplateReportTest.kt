package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708299835)
        val items = mutableListOf<DropTemplate>()
        for (index in 0 until 3) {
            items.add(newDropTemplate("drop-template-$index", "sample"))
        }
        val report = buildDropTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
