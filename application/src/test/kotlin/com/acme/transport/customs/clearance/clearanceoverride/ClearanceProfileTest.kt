package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceProfileTest {
    @Test
    fun `starts in created`() {
        val item = newClearanceProfile("code-457", "label-199")
        assertEquals(ClearanceProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newClearanceProfile("code-457", "label-199")
        try {
            item.moveTo(ClearanceProfileStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceProfileStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newClearanceProfile("code-457", "label-199")
        item.status = ClearanceProfileStatus.RETIRED
        assertFails("move") { item.moveTo(ClearanceProfileStatus.CREATED) }
    }
}
