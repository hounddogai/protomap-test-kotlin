package com.acme.transport.network.hub.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyHubRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyHubRecordEngine(newRegistry())
        val item = newLegacyHubRecord("code-452", "title-373")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
