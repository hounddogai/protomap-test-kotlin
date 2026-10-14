package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveQualificationProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveQualificationProposalStore()
        val item = newAdaptiveQualificationProposal("code-237", "title-699")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
