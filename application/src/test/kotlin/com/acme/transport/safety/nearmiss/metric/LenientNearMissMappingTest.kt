package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNearMissMappingTest {
    @Test
    fun `starts in open`() {
        val item = newLenientNearMissMapping("code-835", "name-239")
        assertEquals(LenientNearMissMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLenientNearMissMapping("code-835", "name-239")
        try {
            item.moveTo(LenientNearMissMappingStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNearMissMappingStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientNearMissMapping("code-835", "name-239")
        item.status = LenientNearMissMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientNearMissMappingStatus.OPEN) }
    }
}
