package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubPreference("code-586", "name-218")
        assertEquals(HubPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newHubPreference("code-586", "name-218")
        try {
            item.moveTo(HubPreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubPreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubPreference("code-586", "name-218")
        item.status = HubPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubPreferenceStatus.PROPOSED) }
    }
}
