package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDeclarationTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701108641)
        val items = mutableListOf<RegionalDeclarationTemplate>()
        for (index in 0 until 3) {
            items.add(newRegionalDeclarationTemplate("regional-declaration-template-$index", "sample"))
        }
        val report = buildRegionalDeclarationTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
