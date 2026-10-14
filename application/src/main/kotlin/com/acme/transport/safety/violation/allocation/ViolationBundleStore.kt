package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation bundles by code. */
interface ViolationBundleStore {
    fun find(code: String): ViolationBundle?
    fun save(item: ViolationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationBundleStore(
    var items: MutableMap<String, ViolationBundle>,
    var order: MutableList<String>,
) : ViolationBundleStore {
    override fun find(code: String): ViolationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationBundle) {
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

    override fun list(): MutableList<ViolationBundle> {
        val out = mutableListOf<ViolationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationBundleStore(): ViolationBundleStore {
    return MemoryViolationBundleStore(mutableMapOf<String, ViolationBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationBundleStore(
    var inner: ViolationBundleStore,
    var cache: TtlCache<ViolationBundle>,
    var hits: Int,
    var misses: Int,
) : ViolationBundleStore {
    override fun find(code: String): ViolationBundle? {
        val hit = (cache.get(code) as ViolationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationBundle> = inner.list()
}

fun newCachedViolationBundleStore(inner: ViolationBundleStore, ttl: Int, clock: Clock): ViolationBundleStore {
    return CachedViolationBundleStore(
        inner = inner,
        cache = newTtlCache<ViolationBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation bundles. Empty fields match all. */
data class ViolationBundleQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationBundle>): MutableList<ViolationBundle> {
        val matched = filter(items) { candidate: ViolationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationBundles(store: ViolationBundleStore, query: ViolationBundleQuery): MutableList<ViolationBundle> {
    return query.apply(store.list())
}
