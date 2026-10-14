package com.acme.transport.crewing.qualification.rule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingQualificationRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708859941)
        val items = mutableListOf<RollingQualificationRule>()
        for (index in 0 until 3) {
            items.add(newRollingQualificationRule("rolling-qualification-rule-$index", "sample"))
        }
        val report = buildRollingQualificationRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
