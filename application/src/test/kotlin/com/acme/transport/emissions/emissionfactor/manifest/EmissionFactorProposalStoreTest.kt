package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorProposalStore()
        val item = newEmissionFactorProposal("code-386", "title-6")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
