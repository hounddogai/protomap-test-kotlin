package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCoachingTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCoachingTemplateStore()
        val item = newWeightedCoachingTemplate("code-554", "name-641")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
