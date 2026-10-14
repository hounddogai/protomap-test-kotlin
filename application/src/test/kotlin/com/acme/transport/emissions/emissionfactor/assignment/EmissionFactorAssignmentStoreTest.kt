package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorAssignmentStore()
        val item = newEmissionFactorAssignment("code-245", "name-936")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
