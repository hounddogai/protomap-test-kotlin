package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedParcelTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedParcelTargetStore()
        val item = newWeightedParcelTarget("code-973", "name-740")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
