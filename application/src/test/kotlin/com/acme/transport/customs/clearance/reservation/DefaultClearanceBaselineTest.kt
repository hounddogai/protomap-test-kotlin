package com.acme.transport.customs.clearance.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultClearanceBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultClearanceBaseline("code-763", "name-393")
        assertEquals(DefaultClearanceBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDefaultClearanceBaseline("code-763", "name-393")
        try {
            item.moveTo(DefaultClearanceBaselineStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultClearanceBaselineStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultClearanceBaseline("code-763", "name-393")
        item.status = DefaultClearanceBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultClearanceBaselineStatus.OPEN) }
    }
}
