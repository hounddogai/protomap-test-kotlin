package com.acme.transport.network.hub.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalHubPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalHubPolicy("code-743", "title-94")
        assertEquals(GlobalHubPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalHubPolicy("code-743", "title-94")
        try {
            item.moveTo(GlobalHubPolicyStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalHubPolicyStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalHubPolicy("code-743", "title-94")
        item.status = GlobalHubPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalHubPolicyStatus.PROPOSED) }
    }
}
