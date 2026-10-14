package com.acme.transport.customs.declaration.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDeclarationAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultDeclarationAllocation("code-319", "label-208")
        assertEquals(DefaultDeclarationAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefaultDeclarationAllocation("code-319", "label-208")
        try {
            item.moveTo(DefaultDeclarationAllocationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDeclarationAllocationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultDeclarationAllocation("code-319", "label-208")
        item.status = DefaultDeclarationAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultDeclarationAllocationStatus.PROPOSED) }
    }
}
