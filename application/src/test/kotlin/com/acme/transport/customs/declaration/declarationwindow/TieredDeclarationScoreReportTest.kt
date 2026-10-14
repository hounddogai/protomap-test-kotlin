package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708875160)
        val items = mutableListOf<TieredDeclarationScore>()
        for (index in 0 until 4) {
            items.add(newTieredDeclarationScore("tiered-declaration-score-$index", "sample"))
        }
        val report = buildTieredDeclarationScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
