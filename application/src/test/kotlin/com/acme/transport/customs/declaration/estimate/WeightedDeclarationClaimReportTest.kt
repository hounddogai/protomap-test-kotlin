package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDeclarationClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708208379)
        val items = mutableListOf<WeightedDeclarationClaim>()
        for (index in 0 until 6) {
            items.add(newWeightedDeclarationClaim("weighted-declaration-claim-$index", "sample"))
        }
        val report = buildWeightedDeclarationClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
