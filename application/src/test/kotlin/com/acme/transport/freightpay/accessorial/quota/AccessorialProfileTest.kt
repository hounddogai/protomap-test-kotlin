package com.acme.transport.freightpay.accessorial.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newAccessorialProfile("code-219", "name-459")
        assertEquals(AccessorialProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newAccessorialProfile("code-219", "name-459")
        try {
            item.moveTo(AccessorialProfileStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialProfileStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAccessorialProfile("code-219", "name-459")
        item.status = AccessorialProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AccessorialProfileStatus.PROPOSED) }
    }
}
