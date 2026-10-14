package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global qualification bundles by code. */
interface GlobalQualificationBundleStore {
    fun find(code: String): GlobalQualificationBundle?
    fun save(item: GlobalQualificationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalQualificationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalQualificationBundleStore(
    var items: MutableMap<String, GlobalQualificationBundle>,
    var order: MutableList<String>,
) : GlobalQualificationBundleStore {
    override fun find(code: String): GlobalQualificationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalQualificationBundle) {
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

    override fun list(): MutableList<GlobalQualificationBundle> {
        val out = mutableListOf<GlobalQualificationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalQualificationBundleStore(): GlobalQualificationBundleStore {
    return MemoryGlobalQualificationBundleStore(
        mutableMapOf<String, GlobalQualificationBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalQualificationBundleStore(
    var inner: GlobalQualificationBundleStore,
    var cache: TtlCache<GlobalQualificationBundle>,
    var hits: Int,
    var misses: Int,
) : GlobalQualificationBundleStore {
    override fun find(code: String): GlobalQualificationBundle? {
        val hit = (cache.get(code) as GlobalQualificationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalQualificationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalQualificationBundle> = inner.list()
}

fun newCachedGlobalQualificationBundleStore(inner: GlobalQualificationBundleStore, ttl: Int, clock: Clock): GlobalQualificationBundleStore {
    return CachedGlobalQualificationBundleStore(
        inner = inner,
        cache = newTtlCache<GlobalQualificationBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global qualification bundles. Empty fields match all. */
data class GlobalQualificationBundleQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalQualificationBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalQualificationBundle>): MutableList<GlobalQualificationBundle> {
        val matched = filter(items) { candidate: GlobalQualificationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalQualificationBundles(store: GlobalQualificationBundleStore, query: GlobalQualificationBundleQuery): MutableList<GlobalQualificationBundle> {
    return query.apply(store.list())
}
