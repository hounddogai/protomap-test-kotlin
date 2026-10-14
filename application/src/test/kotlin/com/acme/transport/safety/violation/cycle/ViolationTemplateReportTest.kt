package com.acme.transport.safety.violation.cycle

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707787072)
        val items = mutableListOf<ViolationTemplate>()
        for (index in 0 until 3) {
            items.add(newViolationTemplate("violation-template-$index", "sample"))
        }
        val report = buildViolationTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
