package com.acme.transport.emissions.idling.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryIdlingBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryIdlingBundle("code-111", "label-317")
        assertEquals(PrimaryIdlingBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryIdlingBundle("code-111", "label-317")
        try {
            item.moveTo(PrimaryIdlingBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryIdlingBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryIdlingBundle("code-111", "label-317")
        item.status = PrimaryIdlingBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryIdlingBundleStatus.REQUESTED) }
    }
}
