package com.acme.transport.safety.nearmiss.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newNearMissProfile("code-972", "name-348")
        assertEquals(NearMissProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newNearMissProfile("code-972", "name-348")
        try {
            item.moveTo(NearMissProfileStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissProfileStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNearMissProfile("code-972", "name-348")
        item.status = NearMissProfileStatus.CLOSED
        assertFails("move") { item.moveTo(NearMissProfileStatus.DRAFT) }
    }
}
