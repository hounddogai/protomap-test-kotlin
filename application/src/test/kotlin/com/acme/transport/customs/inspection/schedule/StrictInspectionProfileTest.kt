package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictInspectionProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictInspectionProfile("code-112", "name-277")
        assertEquals(StrictInspectionProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictInspectionProfile("code-112", "name-277")
        try {
            item.moveTo(StrictInspectionProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictInspectionProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictInspectionProfile("code-112", "name-277")
        item.status = StrictInspectionProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictInspectionProfileStatus.REQUESTED) }
    }
}
