package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientParcelMappingTest {
    @Test
    fun `starts in open`() {
        val item = newLenientParcelMapping("code-651", "name-519")
        assertEquals(LenientParcelMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientParcelMapping("code-651", "name-519")
        try {
            item.moveTo(LenientParcelMappingStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientParcelMappingStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientParcelMapping("code-651", "name-519")
        item.status = LenientParcelMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientParcelMappingStatus.OPEN) }
    }
}
