package com.acme.transport.linehaul.tender.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTenderTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedTenderTemplate("code-607", "name-781")
        assertEquals(WeightedTenderTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedTenderTemplate("code-607", "name-781")
        try {
            item.moveTo(WeightedTenderTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTenderTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedTenderTemplate("code-607", "name-781")
        item.status = WeightedTenderTemplateStatus.FAILED
        assertFails("move") { item.moveTo(WeightedTenderTemplateStatus.NEW) }
    }
}
