package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling cutoff drafts by code. */
interface RollingCutoffDraftStore {
    fun find(code: String): RollingCutoffDraft?
    fun save(item: RollingCutoffDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCutoffDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCutoffDraftStore(
    var items: MutableMap<String, RollingCutoffDraft>,
    var order: MutableList<String>,
) : RollingCutoffDraftStore {
    override fun find(code: String): RollingCutoffDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCutoffDraft) {
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

    override fun list(): MutableList<RollingCutoffDraft> {
        val out = mutableListOf<RollingCutoffDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCutoffDraftStore(): RollingCutoffDraftStore {
    return MemoryRollingCutoffDraftStore(mutableMapOf<String, RollingCutoffDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCutoffDraftStore(
    var inner: RollingCutoffDraftStore,
    var cache: TtlCache<RollingCutoffDraft>,
    var hits: Int,
    var misses: Int,
) : RollingCutoffDraftStore {
    override fun find(code: String): RollingCutoffDraft? {
        val hit = (cache.get(code) as RollingCutoffDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCutoffDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCutoffDraft> = inner.list()
}

fun newCachedRollingCutoffDraftStore(inner: RollingCutoffDraftStore, ttl: Int, clock: Clock): RollingCutoffDraftStore {
    return CachedRollingCutoffDraftStore(
        inner = inner,
        cache = newTtlCache<RollingCutoffDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling cutoff drafts. Empty fields match all. */
data class RollingCutoffDraftQuery(
    var locale: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCutoffDraft): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<RollingCutoffDraft>): MutableList<RollingCutoffDraft> {
        val matched = filter(items) { candidate: RollingCutoffDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCutoffDrafts(store: RollingCutoffDraftStore, query: RollingCutoffDraftQuery): MutableList<RollingCutoffDraft> {
    return query.apply(store.list())
}
