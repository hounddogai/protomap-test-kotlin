package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAccessorialOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryAccessorialOverride("code-11", "name-352")
        assertEquals(PrimaryAccessorialOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryAccessorialOverride("code-11", "name-352")
        try {
            item.moveTo(PrimaryAccessorialOverrideStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAccessorialOverrideStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryAccessorialOverride("code-11", "name-352")
        item.status = PrimaryAccessorialOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryAccessorialOverrideStatus.PROPOSED) }
    }
}
