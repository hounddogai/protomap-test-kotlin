package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCutoffPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingCutoffPreference("code-509", "name-171")
        assertEquals(RollingCutoffPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRollingCutoffPreference("code-509", "name-171")
        try {
            item.moveTo(RollingCutoffPreferenceStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCutoffPreferenceStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingCutoffPreference("code-509", "name-171")
        item.status = RollingCutoffPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingCutoffPreferenceStatus.PROPOSED) }
    }
}
