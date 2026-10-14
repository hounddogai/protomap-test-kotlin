package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDeclarationTierTest {
    @Test
    fun `starts in new`() {
        val item = newRollingDeclarationTier("code-63", "name-449")
        assertEquals(RollingDeclarationTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingDeclarationTier("code-63", "name-449")
        try {
            item.moveTo(RollingDeclarationTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDeclarationTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingDeclarationTier("code-63", "name-449")
        item.status = RollingDeclarationTierStatus.FAILED
        assertFails("move") { item.moveTo(RollingDeclarationTierStatus.NEW) }
    }
}
