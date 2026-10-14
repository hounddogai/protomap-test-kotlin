package com.acme.transport.linehaul.backhaul.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newBackhaulGroup("code-473", "title-84")
        assertEquals(BackhaulGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBackhaulGroup("code-473", "title-84")
        try {
            item.moveTo(BackhaulGroupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulGroupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBackhaulGroup("code-473", "title-84")
        item.status = BackhaulGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BackhaulGroupStatus.PROPOSED) }
    }
}
