package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual camera event policies by code. */
interface ManualCameraEventPolicyStore {
    fun find(code: String): ManualCameraEventPolicy?
    fun save(item: ManualCameraEventPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCameraEventPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCameraEventPolicyStore(
    var items: MutableMap<String, ManualCameraEventPolicy>,
    var order: MutableList<String>,
) : ManualCameraEventPolicyStore {
    override fun find(code: String): ManualCameraEventPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCameraEventPolicy) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<ManualCameraEventPolicy> {
        val out = mutableListOf<ManualCameraEventPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCameraEventPolicyStore(): ManualCameraEventPolicyStore {
    return MemoryManualCameraEventPolicyStore(
        mutableMapOf<String, ManualCameraEventPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCameraEventPolicyStore(
    var inner: ManualCameraEventPolicyStore,
    var cache: TtlCache<ManualCameraEventPolicy>,
    var hits: Int,
    var misses: Int,
) : ManualCameraEventPolicyStore {
    override fun find(code: String): ManualCameraEventPolicy? {
        val hit = (cache.get(code) as ManualCameraEventPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCameraEventPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCameraEventPolicy> = inner.list()
}

fun newCachedManualCameraEventPolicyStore(inner: ManualCameraEventPolicyStore, ttl: Int, clock: Clock): ManualCameraEventPolicyStore {
    return CachedManualCameraEventPolicyStore(
        inner = inner,
        cache = newTtlCache<ManualCameraEventPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual camera event policies. Empty fields match all. */
data class ManualCameraEventPolicyQuery(
    var source: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCameraEventPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCameraEventPolicy>): MutableList<ManualCameraEventPolicy> {
        val matched = filter(items) { candidate: ManualCameraEventPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCameraEventPolicies(store: ManualCameraEventPolicyStore, query: ManualCameraEventPolicyQuery): MutableList<ManualCameraEventPolicy> {
    return query.apply(store.list())
}
