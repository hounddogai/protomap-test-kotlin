package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration drafts by code. */
interface DeclarationDraftStore {
    fun find(code: String): DeclarationDraft?
    fun save(item: DeclarationDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationDraftStore(
    var items: MutableMap<String, DeclarationDraft>,
    var order: MutableList<String>,
) : DeclarationDraftStore {
    override fun find(code: String): DeclarationDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationDraft) {
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

    override fun list(): MutableList<DeclarationDraft> {
        val out = mutableListOf<DeclarationDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationDraftStore(): DeclarationDraftStore {
    return MemoryDeclarationDraftStore(mutableMapOf<String, DeclarationDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationDraftStore(
    var inner: DeclarationDraftStore,
    var cache: TtlCache<DeclarationDraft>,
    var hits: Int,
    var misses: Int,
) : DeclarationDraftStore {
    override fun find(code: String): DeclarationDraft? {
        val hit = (cache.get(code) as DeclarationDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationDraft> = inner.list()
}

fun newCachedDeclarationDraftStore(inner: DeclarationDraftStore, ttl: Int, clock: Clock): DeclarationDraftStore {
    return CachedDeclarationDraftStore(
        inner = inner,
        cache = newTtlCache<DeclarationDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration drafts. Empty fields match all. */
data class DeclarationDraftQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationDraft): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationDraft>): MutableList<DeclarationDraft> {
        val matched = filter(items) { candidate: DeclarationDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationDrafts(store: DeclarationDraftStore, query: DeclarationDraftQuery): MutableList<DeclarationDraft> {
    return query.apply(store.list())
}
