package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryIdlingProfileTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryIdlingProfile("code-421", "title-943")
        assertEquals(PrimaryIdlingProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newPrimaryIdlingProfile("code-421", "title-943")
        try {
            item.moveTo(PrimaryIdlingProfileStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryIdlingProfileStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryIdlingProfile("code-421", "title-943")
        item.status = PrimaryIdlingProfileStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryIdlingProfileStatus.NEW) }
    }
}
