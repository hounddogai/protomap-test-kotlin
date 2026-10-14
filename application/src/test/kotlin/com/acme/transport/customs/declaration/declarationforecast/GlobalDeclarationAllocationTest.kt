package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDeclarationAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalDeclarationAllocation("code-23", "title-588")
        assertEquals(GlobalDeclarationAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalDeclarationAllocation("code-23", "title-588")
        try {
            item.moveTo(GlobalDeclarationAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDeclarationAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalDeclarationAllocation("code-23", "title-588")
        item.status = GlobalDeclarationAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalDeclarationAllocationStatus.IDLE) }
    }
}
