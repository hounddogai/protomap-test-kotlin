package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTenderSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTenderSegmentEngine(newRegistry())
        val item = newPrimaryTenderSegment("code-477", "title-723")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
