package com.acme.transport.customs.clearance.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newClearanceBatch("code-265", "name-933")
        assertEquals(ClearanceBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newClearanceBatch("code-265", "name-933")
        try {
            item.moveTo(ClearanceBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newClearanceBatch("code-265", "name-933")
        item.status = ClearanceBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ClearanceBatchStatus.PROPOSED) }
    }
}
