package com.acme.transport.crewing.qualification.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalQualificationBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalQualificationBundle("code-555", "name-306")
        assertEquals(GlobalQualificationBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newGlobalQualificationBundle("code-555", "name-306")
        try {
            item.moveTo(GlobalQualificationBundleStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalQualificationBundleStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalQualificationBundle("code-555", "name-306")
        item.status = GlobalQualificationBundleStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalQualificationBundleStatus.DRAFT) }
    }
}
