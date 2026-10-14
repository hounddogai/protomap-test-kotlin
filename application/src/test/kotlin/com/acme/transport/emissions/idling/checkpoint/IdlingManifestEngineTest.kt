package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingManifestEngine(newRegistry())
        val item = newIdlingManifest("code-66", "title-10")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
