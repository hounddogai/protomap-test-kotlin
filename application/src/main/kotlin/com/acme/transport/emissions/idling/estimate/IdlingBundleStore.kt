package com.acme.transport.emissions.idling.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling bundles by code. */
interface IdlingBundleStore {
    fun find(code: String): IdlingBundle?
    fun save(item: IdlingBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingBundleStore(
    var items: MutableMap<String, IdlingBundle>,
    var order: MutableList<String>,
) : IdlingBundleStore {
    override fun find(code: String): IdlingBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingBundle) {
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

    override fun list(): MutableList<IdlingBundle> {
        val out = mutableListOf<IdlingBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingBundleStore(): IdlingBundleStore {
    return MemoryIdlingBundleStore(mutableMapOf<String, IdlingBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingBundleStore(
    var inner: IdlingBundleStore,
    var cache: TtlCache<IdlingBundle>,
    var hits: Int,
    var misses: Int,
) : IdlingBundleStore {
    override fun find(code: String): IdlingBundle? {
        val hit = (cache.get(code) as IdlingBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingBundle> = inner.list()
}

fun newCachedIdlingBundleStore(inner: IdlingBundleStore, ttl: Int, clock: Clock): IdlingBundleStore {
    return CachedIdlingBundleStore(
        inner = inner,
        cache = newTtlCache<IdlingBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling bundles. Empty fields match all. */
data class IdlingBundleQuery(
    var lane: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingBundle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingBundle>): MutableList<IdlingBundle> {
        val matched = filter(items) { candidate: IdlingBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingBundles(store: IdlingBundleStore, query: IdlingBundleQuery): MutableList<IdlingBundle> {
    return query.apply(store.list())
}
