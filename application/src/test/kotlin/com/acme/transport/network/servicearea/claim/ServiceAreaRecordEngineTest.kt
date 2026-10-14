package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaRecordEngine(newRegistry())
        val item = newServiceAreaRecord("code-82", "title-282")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
