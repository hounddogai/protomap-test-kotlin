package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulBundleTest {
    @Test
    fun `starts in open`() {
        val item = newBackhaulBundle("code-541", "label-602")
        assertEquals(BackhaulBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBackhaulBundle("code-541", "label-602")
        try {
            item.moveTo(BackhaulBundleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulBundleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBackhaulBundle("code-541", "label-602")
        item.status = BackhaulBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(BackhaulBundleStatus.OPEN) }
    }
}
