package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTenderExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedTenderException("code-566", "name-121")
        assertEquals(WeightedTenderExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedTenderException("code-566", "name-121")
        try {
            item.moveTo(WeightedTenderExceptionStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTenderExceptionStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedTenderException("code-566", "name-121")
        item.status = WeightedTenderExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedTenderExceptionStatus.CREATED) }
    }
}
