package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newBackhaulOverride("code-987", "name-929")
        assertEquals(BackhaulOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBackhaulOverride("code-987", "name-929")
        try {
            item.moveTo(BackhaulOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBackhaulOverride("code-987", "name-929")
        item.status = BackhaulOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(BackhaulOverrideStatus.CREATED) }
    }
}
