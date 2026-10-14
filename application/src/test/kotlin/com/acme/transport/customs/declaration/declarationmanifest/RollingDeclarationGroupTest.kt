package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDeclarationGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingDeclarationGroup("code-13", "title-914")
        assertEquals(RollingDeclarationGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRollingDeclarationGroup("code-13", "title-914")
        try {
            item.moveTo(RollingDeclarationGroupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDeclarationGroupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingDeclarationGroup("code-13", "title-914")
        item.status = RollingDeclarationGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingDeclarationGroupStatus.PLANNED) }
    }
}
