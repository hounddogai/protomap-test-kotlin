package com.acme.transport.linehaul.tender.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTenderVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newManualTenderVariance("code-930", "label-833")
        assertEquals(ManualTenderVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualTenderVariance("code-930", "label-833")
        try {
            item.moveTo(ManualTenderVarianceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTenderVarianceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualTenderVariance("code-930", "label-833")
        item.status = ManualTenderVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(ManualTenderVarianceStatus.CREATED) }
    }
}
