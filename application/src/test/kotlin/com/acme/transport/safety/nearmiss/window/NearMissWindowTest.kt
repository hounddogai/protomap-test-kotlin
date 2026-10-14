package com.acme.transport.safety.nearmiss.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newNearMissWindow("code-970", "title-5")
        assertEquals(NearMissWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newNearMissWindow("code-970", "title-5")
        try {
            item.moveTo(NearMissWindowStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissWindowStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newNearMissWindow("code-970", "title-5")
        item.status = NearMissWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(NearMissWindowStatus.REQUESTED) }
    }
}
