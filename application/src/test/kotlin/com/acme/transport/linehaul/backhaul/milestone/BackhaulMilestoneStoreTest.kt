package com.acme.transport.linehaul.backhaul.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulMilestoneStore()
        val item = newBackhaulMilestone("code-573", "label-294")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
