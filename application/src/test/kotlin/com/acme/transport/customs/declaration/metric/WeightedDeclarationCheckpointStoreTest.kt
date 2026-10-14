package com.acme.transport.customs.declaration.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDeclarationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDeclarationCheckpointStore()
        val item = newWeightedDeclarationCheckpoint("code-519", "name-961")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
