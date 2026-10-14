package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledIdlingWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledIdlingWindowStore()
        val item = newScheduledIdlingWindow("code-129", "name-134")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
